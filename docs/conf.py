# coding: utf-8

source_suffix = {
    '.rst': 'restructuredtext',
    '.txt': 'markdown',
    '.md': 'markdown',
}
source_encoding = 'utf-8'
master_doc = 'index'

extensions = ['myst_parser']
myst_enable_extensions = ['colon_fence', 'strikethrough']

project = 'enioka Scan'
copyright = '2017-2023, enioka Haute Couture'
#version = '2.4'
#release = '2.4.1'
hightlight_language = 'java'

html_theme = 'sphinx_rtd_theme'
