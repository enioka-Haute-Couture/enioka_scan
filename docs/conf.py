# coding: utf-8

source_suffix = {
    '.md': 'markdown',
}
source_encoding = 'utf-8'
master_doc = 'index'

extensions = [
    'myst_parser',  # Markdown support
]

myst_enable_extensions = [
    'colon_fence',  # Alternative directive call using :::{directive} :::
    'strikethrough',  # Text strikethrough using ~~text~~
    'fieldlist',  # Better handling of manyfields blocks, useful for API documentation
]

myst_heading_anchors = 4

suppress_warnings = [
    'myst.strikethrough',  # We only generate HTML docs, ignore non-html warnings.
    'myst.xref_missing',  # Re-enable for debugging and check individually, but anchor links are not correctly recognized and will generate false-positive warnings.
]

project = 'enioka Scan'
copyright = '2017-2023, enioka Haute Couture'
#version = '2.4'
#release = '2.4.1'
hightlight_language = 'java'

html_theme = 'sphinx_rtd_theme'
