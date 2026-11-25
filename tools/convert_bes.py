#!/usr/bin/env python3
"""Convierte Biblia en Español Sencillo (USFM, eBible.org spabes, dominio
público, edición 2026) al mismo formato JSON por libro que usa Biblia SL
en assets/bible/.

Uso:
    python3 tools/convert_bes.py /tmp/bes <salida>
    # /tmp/bes debe contener los 66 archivos *.usfm de
    # https://ebible.org/Scriptures/spabes_usfm.zip

Formato de salida por libro (igual que assets/bible/GEN.json):
    {"id", "name", "testament", "source": "bes",
     "sourceLabel": "Biblia en Español Sencillo (dominio público)",
     "chapterSources": ["bes", ...], "chapters": [[versículo, ...], ...]}

Reglas de limpieza USFM:
  - \\w palabra|strong="..." \\w*  -> palabra (se conserva el texto).
  - \\f ... \\f* (notas al pie) y \\x ... \\x* (referencias): se elimina
    el contenido COMPLETO, no solo el marcador, para que el texto de la
    nota no quede dentro del versículo. (Verificado: esta fuente no trae
    \\f ni \\x, pero la regla queda por seguridad.)
  - \\qs Selah \\qs* (Salmos): se eliminan solo los marcadores y se
    conserva la palabra "Selah", igual que hizo el conversor de RV1909
    (el JSON generado de RV1909 contiene "Selah" 70 veces en Salmos).
  - Cualquier otro marcador que aparezca dentro de un versículo se
    elimina y se registra en stderr.
  - Los deuterocanónicos no existen en BES: no se generan
    (TOB, JDT, WIS, SIR, BAR, 1MA, 2MA). Daniel sale con 12 capítulos;
    la app añade Daniel 13-14 desde la Santa Biblia libre (spablm),
    igual que hace con la VBL y la RV1909.
"""
import json
import re
import sys
from pathlib import Path

SOURCE = "bes"
SOURCE_LABEL = "Biblia en Español Sencillo (dominio público)"

# Nombres y testamento del canon, tomados de assets/bible/index.json
# (se pasan como argumento para no duplicar la lista aquí).


def clean_verse(raw: str) -> str:
    # \f ... \f* (notas al pie) y \x ... \x* (referencias): se elimina el
    # contenido COMPLETO para que el texto de la nota no quede en el verso.
    text = re.sub(r'\\f\b.*?\\f\*', ' ', raw)
    text = re.sub(r'\\x\b.*?\\x\*', ' ', text)
    # \w ...|strong="..." \w*  ->  el texto antes de |
    text = re.sub(r'\\\+?w\s+(.*?)\|strong="[^"]*"\\\+?w\*', r'\1', text)
    # \add ... \add* -> texto
    text = re.sub(r'\\add\s+(.*?)\\add\*', r'\1', text)
    # Cualquier marcador restante dentro del versículo: avisar y quitarlo.
    leftover = re.findall(r'\\[a-zA-Z0-9]+\*?', text)
    if leftover:
        print(f"AVISO marcador restante en versículo: {leftover} :: {raw[:120]!r}",
              file=sys.stderr)
    text = re.sub(r'\\[a-zA-Z0-9]+\*?', ' ', text)
    text = re.sub(r'\s+', ' ', text).strip()
    return text


def parse_usfm(path: Path):
    """Devuelve (book_id, chapters) donde chapters es lista de listas
    de versículos (listas de str, en orden, numeración 1..N)."""
    text = path.read_text(encoding='utf-8')
    m = re.search(r'\\id\s+(\S+)', text)
    book_id = m.group(1)
    chapters: list[list[str]] = []
    cur_chapter: list[str] | None = None
    cur_verse_no: int | None = None
    cur_parts: list[str] = []

    def flush_verse():
        nonlocal cur_verse_no, cur_parts
        if cur_verse_no is not None and cur_chapter is not None:
            # La numeración debe ser 1, 2, 3... sin huecos.
            expected = len(cur_chapter) + 1
            if cur_verse_no != expected:
                print(f"AVISO {book_id}: versículo {cur_verse_no} "
                      f"cuando se esperaba {expected}", file=sys.stderr)
                while len(cur_chapter) < cur_verse_no - 1:
                    cur_chapter.append("")
            cur_chapter.append(clean_verse(' '.join(cur_parts)))
        cur_verse_no = None
        cur_parts = []

    for line in text.splitlines():
        line = line.strip()
        if not line:
            continue
        cm = re.match(r'\\c\s+(\d+)', line)
        if cm:
            flush_verse()
            chapters.append([])
            cur_chapter = chapters[-1]
            continue
        vm = re.match(r'\\v\s+(\d+)\s*(.*)', line)
        if vm:
            flush_verse()
            cur_verse_no = int(vm.group(1))
            cur_parts = [vm.group(2)] if vm.group(2) else []
            continue
        if line.startswith('\\'):
            # Marcadores de párrafo/título fuera de versículo: se ignoran.
            continue
        # Continuación de un versículo en la línea siguiente.
        if cur_verse_no is not None:
            cur_parts.append(line)
        else:
            print(f"AVISO {book_id}: línea fuera de versículo: {line[:80]!r}",
                  file=sys.stderr)
    flush_verse()
    return book_id, chapters


def main():
    usfm_dir = Path(sys.argv[1])
    out_dir = Path(sys.argv[2])
    index_path = Path(sys.argv[3]) if len(sys.argv) > 3 else None
    index = json.loads(index_path.read_text(encoding='utf-8')) if index_path else None
    info_by_id = {b["id"]: b for b in index["books"]} if index else {}

    out_dir.mkdir(parents=True, exist_ok=True)
    total_verses = 0
    total_chapters = 0
    books_written = 0
    bes_index = {"books": []}
    for usfm in sorted(usfm_dir.glob("*.usfm")):
        book_id, chapters = parse_usfm(usfm)
        info = info_by_id.get(book_id)
        name = info["name"] if info else book_id
        testament = info["testament"] if info else "AT"
        doc = {
            "id": book_id,
            "name": name,
            "testament": testament,
            "source": SOURCE,
            "sourceLabel": SOURCE_LABEL,
            "chapterSources": [SOURCE] * len(chapters),
            "chapters": chapters,
        }
        (out_dir / f"{book_id}.json").write_text(
            json.dumps(doc, ensure_ascii=False), encoding='utf-8')
        n_verses = sum(len(c) for c in chapters)
        total_verses += n_verses
        total_chapters += len(chapters)
        books_written += 1
        bes_index["books"].append({
            "id": book_id, "name": name, "testament": testament,
            "chapters": len(chapters), "source": SOURCE,
            "sourceLabel": SOURCE_LABEL,
        })
        print(f"{book_id}: {len(chapters)} caps., {n_verses} vers.")
    (out_dir / "index.json").write_text(
        json.dumps(bes_index, ensure_ascii=False, indent=1), encoding='utf-8')
    print(f"TOTAL: {books_written} libros, {total_chapters} capítulos, "
          f"{total_verses} versículos")


if __name__ == "__main__":
    main()
