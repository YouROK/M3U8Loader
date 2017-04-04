package utils

import (
	"net/url"
	"path/filepath"
	"strings"
)

func isRelativeUrl(path string) bool {
	return !strings.HasPrefix(strings.ToLower(path), "http://") && !strings.HasPrefix(strings.ToLower(path), "https://") && !strings.HasPrefix(strings.ToLower(path), "file://")
}

func DirUrl(Url string) (string, error) {
	uri, err := url.Parse(Url)
	if err != nil {
		return "", err
	}
	uri.Path = filepath.Dir(uri.Path)
	return uri.String(), nil
}

func JoinUrl(BaseUrl, PartUrl string) (string, error) {
	//if not relative return part url
	if !isRelativeUrl(PartUrl) {
		return PartUrl, nil
	}

	uri, err := url.Parse(BaseUrl)
	if err != nil {
		return "", err
	}
	ret := uri.Scheme + "://" + uri.Host + filepath.Join(uri.Path, PartUrl)
	return ret, nil
}
