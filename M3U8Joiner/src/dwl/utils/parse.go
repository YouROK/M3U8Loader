package utils

import (
	//"fmt"
	"net/url"
	"path/filepath"
	"strings"
	"unicode"
)

func isRelativeUrl(path string) bool {
	path = strings.ToLower(path)
	if strings.HasPrefix(path, "http://") || strings.HasPrefix(path, "https://") || strings.HasPrefix(path, "file://") || strings.HasPrefix(path, "/") {
		return false
	}
	return true
}

func DirUrl(Url string) (string, error) {
	uri, err := url.Parse(Url)
	if err != nil {
		return "", err
	}
	uri.Path = filepath.Dir(uri.Path)
	return uri.String(), nil
}

func copyUrlArgs(dst, src string) string {
	dsturl, err := url.Parse(dst)
	if err != nil {
		return dst
	}

	srcurl, err := url.Parse(src)
	if err != nil || srcurl.RawQuery == "" {
		return dst
	}

	dstquery := strings.ToLower(dsturl.RawQuery)
	if dstquery == "" || dstquery == "null=0" {
		dsturl.RawQuery = srcurl.RawQuery
		dsturl.ForceQuery = srcurl.ForceQuery
		dsturl.Fragment = srcurl.Fragment
		return dsturl.String()
	}
	return dst
}

func JoinUrl(BaseUrl, PartUrl string) (string, error) {
	//if not relative return part url
	if !isRelativeUrl(PartUrl) {
		if !strings.HasPrefix(strings.ToLower(PartUrl), "http") {
			Url, err := url.Parse(BaseUrl)
			if err != nil {
				return "", err
			}
			PartUrl = Url.Scheme + "://" + Url.Host + PartUrl
		}
		PartUrl = copyUrlArgs(PartUrl, BaseUrl)
		return PartUrl, nil
	}

	uri, err := url.Parse(BaseUrl)
	if err != nil {
		return "", err
	}
	ret := uri.Scheme + "://" + uri.Host + filepath.Join(uri.Path, PartUrl)
	ret = copyUrlArgs(ret, BaseUrl)
	return ret, nil
}

func IsBinarySafe(buffer []byte) int {
	str := []rune(string(buffer))
	isg := len(str)
	for _, c := range str {
		if unicode.IsGraphic(c) || unicode.IsSpace(c) {
			isg--
		}
	}
	return (isg * 100) / len(str)
}
