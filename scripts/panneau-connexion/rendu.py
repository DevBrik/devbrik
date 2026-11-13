#!/usr/bin/env python3
"""Pré-rend les visuels du panneau de connexion (maquette Penpot, page 4) :
fond dégradé #1E3A8A -> #B1B2B5 transparent sur #F8FAFC, aperçu à (73, 70), rayon 15 px, fusion soft-light.
Toile de 1100 x 800 (rendu x2) : l'aperçu garde l'échelle de la maquette (725 px pour une capture de 900 px,
soit 0,806) mais s'étend jusqu'au bord droit. Le frontend ajuste le visuel à la HAUTEUR du panneau et coupe
à droite : le bas blanc reste toujours visible derrière la phrase, quel que soit l'écran.
Sources : captures de 1300 x 800.
"""
import asyncio, sys, base64
from playwright.async_api import async_playwright
SRC=sys.argv[1]; OUT=sys.argv[2]
NOMS=['publier','briques','tester','demandes','classement']
HTML="""<html><body style="margin:0"><div style="position:relative;width:1100px;height:800px;overflow:hidden;isolation:isolate;
background:linear-gradient(180deg,#1E3A8A 0%,rgba(177,178,181,0) 100%),#F8FAFC">
<img src="data:image/png;base64,{b64}" style="position:absolute;left:73px;top:70px;width:1048px;border-radius:15px;mix-blend-mode:soft-light"></div></body></html>"""
async def main():
    async with async_playwright() as p:
        b=await p.chromium.launch(executable_path='/usr/bin/google-chrome')
        pg=await b.new_page(viewport={'width':1100,'height':800}, device_scale_factor=2)
        for n in NOMS:
            b64=base64.b64encode(open(f'{SRC}/{n}.png','rb').read()).decode()
            await pg.set_content(HTML.format(b64=b64)); await pg.wait_for_timeout(300)
            await pg.screenshot(path=f'{OUT}/{n}.png', clip={'x':0,'y':0,'width':1100,'height':800})
        await b.close()
asyncio.run(main())
