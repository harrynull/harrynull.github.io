---
title: 判断UTF-8字符数
tags:
  - utf-8
categories:
  - 未分类
date: 2018-04-28 21:41:16
---

众所周知（划掉），UTF-8是一种可变长度字符编码，即每个字符会占用不等数量(1-4个)的byte。例如，一个英文字母只占用1个byte，而大部分汉字需要2个byte来储存。

因此，传统的strlen（统计字符串byte数量）来获取字符数的方式就变得不准确了。这就是为什么某些网站输入框会把一个汉字误认为是两个字的原因。

<!--more-->

那么，应该如何正确判断字符数量呢？UTF-8遵循以下编码规则：
```
Binary    Hex          Comments
0xxxxxxx  0x00..0x7F   Only byte of a 1-byte character encoding
10xxxxxx  0x80..0xBF   Continuation bytes (1-3 continuation bytes)
110xxxxx  0xC0..0xDF   First byte of a 2-byte character encoding
1110xxxx  0xE0..0xEF   First byte of a 3-byte character encoding
11110xxx  0xF0..0xF4   First byte of a 4-byte character encoding
```
（摘自 [How many bytes does one Unicode character take?](https://stackoverflow.com/a/33349765/4358404)）

可见，有两种获取字符数的方法：一个是通过第一个byte中前X个是1来判断这个字符一共有几个byte，另一个是直接忽略掉10开头的字符。显然，后者比较容易实现。

```cpp
size_t count_character(char* raw_string) {
    size_t character_counter = 0;
    for (char* ptr = raw_string; *ptr != '\0'; ptr++) {
        if ((((*ptr) >> 6) & 0b11) == 0b10) continue;
        character_counter++;
    }
    return character_counter;
}
```

测试程序：
```cpp
int main() {
    char raw_string[] = { "你好, 世界!" };
    std::cout << "strlen: " << strlen(raw_string) << std::endl;
    std::cout << "count_character: " << count_character(raw_string) << std::endl;
}
```
输出：
```
strlen: 15
count_character: 7
```


写了好久了，一直懒得发上来（