from os import walk, makedirs, path

for r, d, fs in walk('.'):
    if r != '.':
        break
    for f in fs:
        s = f.split('.')
        name = s[0]
        ext = s[1]
        if ext == 'groovy':
            dirpath = "snapshots/{}".format(name)
            if not path.exists(dirpath):
                makedirs(dirpath)
            print("R:{}, D:{}, F:{}".format(r, d, name))
