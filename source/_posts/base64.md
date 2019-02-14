---
title: 用C++实现base64编解码
categories: 未分类
date: 2018-11-07 09:50:00
tags: ["C++", "base64", "编码"]
copyright:
  license: 知识共享 署名-相同方式共享 3.0协议
---

Base64是一种基于64个可打印字符来表示二进制数据的表示方法。由于我好久没写 C++ 了，于是突然一时兴起想要造一个 Base64 编解码的轮子。

<!--more-->

> 转换的时候，将3字节的数据，先后放入一个24位的缓冲区中，先来的字节占高位。数据不足3字节的话，于缓冲器中剩下的比特用0补足。每次取出6比特，按照其值选择`ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/`中的字符作为编码后的输出，直到全部输入数据转换完成。若原数据长度不是3的倍数时且剩下1个输入数据，则在编码结果后加2个=；若剩下2个输入数据，则在编码结果后加1个=。

以上是摘自维基百科[Base64](https://zh.wikipedia.org/wiki/Base64)的 Base64 编码方式。

首先在实现编解码之前先定义 base64 字符。

```cpp
constexpr char base64_char[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
```

## 编码
首先先实现编码函数：

```cpp
string base64_encode(const char* src, size_t len) {
	string ret = "";
    ret.reserve(len * 4 / 3);               // base64编码长度大概是原来的4/3
	for (size_t i = 0; i < len; i += 3) {   // 每三个byte处理一次
		char first = src[i];
		char second = i + 1 < len ? src[i + 1] : 0;
		char third = i + 2 < len ? src[i + 2] : 0;

		ret += base64_char[(first >> 2) & 0b111111]; // 1st high 6 bits
		ret += base64_char[(first & 0b11) << 4 | ((second >> 4) & 0b1111)]; // 1st low 2 bits + 2nd high 4 bits
		if (i + 1 >= len) { ret += "=="; break; }
		ret += base64_char[(second & 0b1111) << 2 | ((third >> 6) & 0b11)]; // 2nd low 4 bits + 3rd high 2 bits
		if (i + 2 >= len) { ret += "="; break; }
		ret += base64_char[third & 0b111111]; // 3rd low 6 bits
	}
	return ret;
}
```

需要注意的是位运算的处理方式：
`(first & 0b11) << 4 | ((second >> 4) & 0b1111)` 这一个表达式将first的最后2bits和second的高4bits连在了一起。需要注意的是`>>`运算符在某些编译器下（implementation-defined）可能会导致执行arithmetic shift而不是logical shift。这就会导致`0b11110000>>1`的结果有可能是`0b11111000`（移位时复制最高位）。因此，在这个操作后要手动`& 0b1111`一下。

## 解码
然后再来实现解码函数。解码函数基本上就是编码的逆向过程。

首先先定义一个`base64_char_convert`函数用来将base64字符转换成原始byte。以下是一个最简单的实现。出于效率考虑还可以把这个函数实现成查表。
```cpp
struct InvalidBase64 {};

char base64_char_convert(char c) {
	for (size_t i = 0; i < sizeof(base64_char); ++i)
		if (base64_char[i] == c) return i;
	throw InvalidBase64(); // Invalid base64 character
}
```

然后再实现真正的解码函数：
```cpp
vector<char> base64_decode(const string& src) {
	vector<char> ret;
    ret.reserve(src.length() * 3 / 4);
	for (size_t i = 0; i < src.length(); i += 4) {
		char b1 = base64_char_convert(src[i]);
		char b2 = base64_char_convert(src[i + 1]);

		ret.push_back((b1 << 2) | ((b2 >> 4 & 0b11))); // 1st 6bits, 2nd high 2bits

		if (src[i + 2] == '=') break;
		char b3 = base64_char_convert(src[i + 2]);
		ret.push_back(((b2 & 0b1111) << 4) | ((b3 >> 2) & 0b1111)); // 2nd low 4bits, 3rd high 4bits

		if (src[i + 3] == '=') break;
		char b4 = base64_char_convert(src[i + 3]);
		ret.push_back(((b3 & 0b11) << 6) | b4); // 3rd low 2bits, 4th 6bits
	}
	return ret;
}
```

## 测试

最后，写一个主程序测试一下以上代码吧。
```cpp
string bytes_to_string(const vector<char>& v) {
	return string(v.begin(), v.end());
}

#define STR_AND_LEN(c_str) c_str, (sizeof(c_str)-1)
int main() {
	string a, b, c;
	cout << (a = base64_encode(STR_AND_LEN("hi, base64!"))) << endl;
	cout << (b = base64_encode(STR_AND_LEN("hii, base64!"))) << endl;
	cout << (c = base64_encode(STR_AND_LEN("hello, base64!"))) << endl;

	cout << (bytes_to_string(base64_decode(a))) << endl;
	cout << (bytes_to_string(base64_decode(b))) << endl;
	cout << (bytes_to_string(base64_decode(c))) << endl;

    cout << base64_encode(STR_AND_LEN(
        "Man is distinguished, not only by his reason, but by this singular passion "
        "from other animals, which is a lust of the mind, that by a perseverance of "
        "delight in the continued and indefatigable generation of knowledge, exceeds "
        "the short vehemence of any carnal pleasure."))<<endl;
	cout << (bytes_to_string(base64_decode(
		"TWFuIGlzIGRpc3Rpbmd1aXNoZWQsIG5vdCBvbmx5IGJ5IGhpcyByZWFzb24sIGJ1dCBieSB0aGlz"
		"IHNpbmd1bGFyIHBhc3Npb24gZnJvbSBvdGhlciBhbmltYWxzLCB3aGljaCBpcyBhIGx1c3Qgb2Yg"
		"dGhlIG1pbmQsIHRoYXQgYnkgYSBwZXJzZXZlcmFuY2Ugb2YgZGVsaWdodCBpbiB0aGUgY29udGlu"
		"dWVkIGFuZCBpbmRlZmF0aWdhYmxlIGdlbmVyYXRpb24gb2Yga25vd2xlZGdlLCBleGNlZWRzIHRo"
		"ZSBzaG9ydCB2ZWhlbWVuY2Ugb2YgYW55IGNhcm5hbCBwbGVhc3VyZS4="))) << endl;
}
```

运行结果：
```
aGksIGJhc2U2NCE=
aGlpLCBiYXNlNjQh
aGVsbG8sIGJhc2U2NCE=
hi, base64!
hii, base64!
hello, base64!
TWFuIGlzIGRpc3Rpbmd1aXNoZWQsIG5vdCBvbmx5IGJ5IGhpcyByZWFzb24sIGJ1dCBieSB0aGlzIHNpbmd1bGFyIHBhc3Npb24gZnJvbSBvdGhlciBhbmltYWxzLCB3aGljaCBpcyBhIGx1c3Qgb2YgdGhlIG1pbmQsIHRoYXQgYnkgYSBwZXJzZXZlcmFuY2Ugb2YgZGVsaWdodCBpbiB0aGUgY29udGludWVkIGFuZCBpbmRlZmF0aWdhYmxlIGdlbmVyYXRpb24gb2Yga25vd2xlZGdlLCBleGNlZWRzIHRoZSBzaG9ydCB2ZWhlbWVuY2Ugb2YgYW55IGNhcm5hbCBwbGVhc3VyZS4=
Man is distinguished, not only by his reason, but by this singular passion from other animals, which is a lust of the mind, that by a perseverance of delight in the continued and indefatigable generation of knowledge, exceeds the short vehemence of any carnal pleasure.
```
