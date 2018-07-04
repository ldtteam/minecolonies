#!/usr/local/bin/python3

import sys
import requests
import os

base_url = "https://poeditor.com/api/"
api_key = ""

def post(data):
    r = requests.post(base_url, data=data)
    r.raise_for_status()
    return r.json()

def get(path):
    r = requests.get(path)
    r.raise_for_status()
    return r.json()

def getLangs():
    return post({"api_token":api_key, "id":"69487", "action":"list_languages"})['list']

def getStrings(lang):
    return get(post({"api_token":api_key, "id":"69487", "action":"export", "type":"key_value_json", "language":lang})['item'])

def getMcLang(poe, mc_langs):
    if len(poe) == 2:
        for lang in mc_langs:
            if lang[:2] == poe:
                return lang
        return poe[:2].lower() + "_" + poe[:2].lower() + ".lang"
    else:
        return poe[:2].lower() + "_" + poe[3:].lower() + ".lang"

if __name__ == "__main__":
    if len(sys.argv) > 1:
        api_key = sys.argv[1]
    else:
        with open(".api_key") as f:
            api_key = f.readline().strip()

    poe_langs = []
    for lang in getLangs():
        poe_langs += [lang['code']]
    poe_langs.sort(key = len, reverse = True)

    mc_langs = os.listdir("../../src/main/resources/assets/minecolonies/lang")

    for poe_lang in poe_langs:
        strings = getStrings(poe_lang)
        mc_lang = getMcLang(poe_lang, mc_langs)
        print(poe_lang)
        print(mc_lang)
        with open("../../src/main/resources/assets/minecolonies/lang/" + mc_lang, 'w') as f:
            for k,v in strings.items():
                if isinstance(v, {}):
                    k = next(iter(v))
                    v = v[k]
                if v == '':
                    f.write('#' + k + '=' + v + '\n')
                else:
                    f.write(k + '=' + v + '\n')
        if mc_lang in mc_langs:
            mc_langs.remove(mc_lang)

    if len(mc_langs) != 0:
        print("We messed up!")
        print("Langs left:")
        print(mc_langs)


