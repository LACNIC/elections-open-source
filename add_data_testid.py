import re
import sys
from pathlib import Path

heading_pattern = re.compile(r'<(h[1-5])([^>]*)>(.*?)</\1>', re.DOTALL)

def sanitize(text):
    text = re.sub(r'<[^>]+>', '', text)
    text = text.strip().lower()
    text = re.sub(r'\s+', '_', text)
    return text[:20]

def heading_sub(match):
    tagname = match.group(1)
    attrs = match.group(2)
    body = match.group(3)
    if 'data-testid' in attrs:
        return match.group(0)
    value = sanitize(body)
    return f'<{tagname}{attrs} data-testid="{value}">{body}</{tagname}>'


def add_data_testid(tag):
    if 'data-testid=' in tag:
        return tag
    m = re.search(r'wicket:id="([^"]+)"', tag)
    if m:
        value = m.group(1)
        return tag.replace(m.group(0), f'{m.group(0)} data-testid="{value}"')
    m = re.search(r'id="([^"]+)"', tag)
    if m:
        value = m.group(1)
        return tag.replace(m.group(0), f'{m.group(0)} data-testid="{value}"')
    m = re.search(r'name="([^"]+)"', tag)
    if m:
        value = m.group(1)
        return tag.replace(m.group(0), f'{m.group(0)} data-testid="{value}"')
    return tag


def process_html(content):
    content = heading_pattern.sub(heading_sub, content)
    return re.sub(r'<[^>]+>', lambda m: add_data_testid(m.group(0)), content)


def main(paths):
    for path in paths:
        for file in Path(path).rglob('*.html'):
            text = file.read_text(encoding='utf-8')
            new_text = process_html(text)
            if new_text != text:
                file.write_text(new_text, encoding='utf-8')

if __name__ == '__main__':
    main(sys.argv[1:])

