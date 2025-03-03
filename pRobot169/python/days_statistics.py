import os
import re

first = {}
last = {}
authors = {}
titles = {}
selfErase = {}
articles = 0
deleted = 0

def get_start_last(stat):
    start = last = None
    with open(stat, 'r') as stat_file:
        for line in stat_file:
            if match := re.match(r'^start = (\d+)', line):
                start = f"{int(match.group(1)):5}"
            if match := re.match(r'^last = (\d+)', line):
                last = f"{int(match.group(1)):5}"
    return start, last

def top_n(p_list, n_top, n_char):
    result = []
    n = 0
    n_last = 0
    for i in sorted(p_list, reverse=True):
        if n < n_top:
            result.append(i)
            n += 1
        n_last = int(i[1:n_char+1]) if n == n_top else n_last
        if int(i[1:n_char+1]) < n_last:
            break
        elif n > n_top:
            result.append(i)
    return '\n'.join(result)

def print_titles():
    global sort_titles
    sort_titles = [f"{titles[i]:6} | {i}" for i in titles]

    titles_normal = []
    titles_popular = []

    for i in sort_titles:
        if re.search(r'[0-9]', i):
            if re.search(r'(推廣|日目|航目)', i):
                titles_popular.append(i)
            else:
                titles_normal.append(i)

    sum_titles_pop = ["111111 | \xFF\xFF"]
    main_titles_pop = {}
    for i in sorted(titles_popular, key=lambda x: x.split("|")[1].strip()):
        n_prev, title_prev = re.match(r'^\s*(\d+)\s\|\s(.+)$', sum_titles_pop[-1]).groups()
        n_cur, title_cur = re.match(r'^\s*(\d+)\s\|\s(.+)$', i).groups()
        ox, title_main = cmp_similar(title_prev, title_cur)
        if ox == "O":
            sum_titles_pop.pop()
            sum_titles_pop.append(f"{int(n_prev)+int(n_cur):6} | {title_prev}")
            main_titles_pop[title_prev] = title_main
        else:
            sum_titles_pop.append(i)
    sum_titles_pop.pop(0)

    for i in sum_titles_pop:
        n_cur, title_cur = re.match(r'^\s*(\d+)\s\|\s(.+)$', i).groups()
        if title_cur in main_titles_pop:
            i = f"{int(n_cur):6} | {main_titles_pop[title_cur]}"

    print("     # |   Titles\n-------+---------------------------------------------")
    print(top_n(titles_normal, 40, 6))
    print("\n推廣成果:\n     # |   Titles\n-------+---------------------------------------------")
    print(top_n(sum_titles_pop, 20, 6))
    print("\n")

def print_authors():
    print("     # |  Authors\n-------+---------------------------------------------")
    global sort_authors
    sort_authors = []
    for i in authors:
        name = i
        name = i[0] + "*" * (len(i) - 2) + i[-1]
        if len(name) == 2:
            name = "*" + name[1]
        sort_authors.append(f"{authors[i]:6} | {name}")
    print(top_n(sort_authors, 30, 6))
    print("\n")

def cmp_similar(a, b):
    maina, mainb = a, b
    main1, main2 = maina, mainb
    if len(main1) > len(main2):
        main1, main2 = main2, main1
    if main2.startswith(main1) and re.match(r'^[a-zA-Z ]{0,3}$', main2[len(main1):]):
        return "O", maina

    a_chars = [char for char in a if char.strip()]
    b_chars = [char for char in b if char.strip()]
    i = 0
    for i in range(len(a_chars)):
        if i > len(b_chars) - 1:
            break
        if a_chars[i] != b_chars[i]:
            if is_num_chi(a_chars[i]) or is_num_chi(b_chars[i]):
                ja = i
                jb = i
                ka = i
                kb = i
                if ja > 0 and is_num_chi(a_chars[ja-1]) and not is_num_chi(a_chars[i]):
                    ja -= 1
                if jb > 0 and is_num_chi(b_chars[jb-1]) and not is_num_chi(b_chars[i]):
                    jb -= 1
                while ja > 0 and is_num_chi(a_chars[ja]):
                    ja -= 1
                while jb > 0 and is_num_chi(b_chars[jb]):
                    jb -= 1
                if ja != jb:
                    return "X", maina
                while ka <= len(a_chars) - 1 and is_num_chi(a_chars[ka]):
                    ka += 1
                while kb <= len(b_chars) - 1 and is_num_chi(b_chars[kb]):
                    kb += 1
                maina = ''.join(a_chars[:ja+1] + ["---"] + a_chars[ka:])
                mainb = ''.join(b_chars[:jb+1] + ["---"] + b_chars[kb:])
                main1, main2 = maina, mainb
                if len(main1) > len(main2):
                    main1, main2 = main2, main1
                if main2.startswith(main1) and re.match(r'^[a-zA-Z ]{0,2}$', main2[len(main1):]):
                    return "O", maina
                else:
                    return "X", maina
            elif re.match(r'\d', a_chars[i]) or re.match(r'\d', b_chars[i]):
                ja = i
                jb = i
                ka = i
                kb = i
                if ja > 0 and re.match(r'\d', a_chars[ja-1]) and not re.match(r'\d', a_chars[i]):
                    ja -= 1
                if jb > 0 and re.match(r'\d', b_chars[jb-1]) and not re.match(r'\d', b_chars[i]):
                    jb -= 1
                while ja > 0 and re.match(r'\d', a_chars[ja]):
                    ja -= 1
                while jb > 0 and re.match(r'\d', b_chars[jb]):
                    jb -= 1
                if ja != jb:
                    return "X", maina
                while ka <= len(a_chars) - 1 and re.match(r'\d', a_chars[ka]):
                    ka += 1
                while kb <= len(b_chars) - 1 and re.match(r'\d', b_chars[kb]):
                    kb += 1
                maina = ''.join(a_chars[:ja+1] + ["---"] + a_chars[ka:])
                mainb = ''.join(b_chars[:jb+1] + ["---"] + b_chars[kb:])
                main1, main2 = maina, mainb
                if len(main1) > len(main2):
                    main1, main2 = main2, main1
                if main2.startswith(main1) and re.match(r'^[a-zA-Z ]{0,2}$', main2[len(main1):]):
                    return "O", maina
                else:
                    return "X", maina
            else:
                return "X", a
    return "X", a

def is_num_chi(c):
    num_chi = {'零', '一', '二', '三', '四', '五', '六', '七', '八', '九', '十', '百', '千', '幾'}
    return c in num_chi

import sys
directories = sys.argv[1:]

for dir in directories:
    if not os.path.isdir(dir):
        continue
    date = ""
    while dir.endswith('/'):
        dir = dir[:-1]
    if match := re.match(r'.*/([^/]+)$', dir):
        date = match.group(1)
    else:
        date = dir
    date = date[:8]
    if not re.match(r'^\d{8}$', date):
        continue
    if not os.path.isfile(f"{dir}/stat.txt"):
        continue
    if not os.path.isfile(f"{dir}/{date}.txt"):
        continue

    start, last = get_start_last(f"{dir}/stat.txt")
    slashdate = f"{date[4:6]}/{date[6:8]}"
    with open(f"{dir}/{date}.txt", 'r') as list_file:
        for line in list_file:
            if line[10:15] != slashdate:
                continue
            if line[1:6] < start:
                continue
            if line[1:6] > last:
                break
            line = line.strip().replace('\r', '').replace('\n', '')
            author = line[16:28].rstrip()
            title = line[32:]
            if line[1:6] == start:
                first[date] = line
            if line[1:6] == last:
                last[date] = line

            authors[author] = authors.get(author, 0) + 1
            if line[7:8].lower() == "d":
                deleted += 1
                if re.search(fr'\({author}\)', title):
                    selfErase[author] = selfErase.get(author, 0) + 1
                continue
            articles += 1
            titles[title] = titles.get(title, 0) + 1

    print(f"{date} {slashdate} {start}-{last}\n")

print_titles()

