package m3u8

import (
	"loader"
	"net/url"
	"path/filepath"
	"strings"

	"github.com/grafov/m3u8"
)

func ParseList(opt *loader.HttpOpts) (*List, error) {
	http := loader.NewHttp(opt)
	err := http.Connect()
	if err != nil {
		return nil, err
	}
	mList, mType, err := m3u8.DecodeFrom(http, false)

	if err != nil {
		return nil, err
	}

	list := new(List)
	list.Url = opt.Url

	switch mType {
	case m3u8.MEDIA:
		l := mList.(*m3u8.MediaPlaylist)
		for _, i := range l.Segments {
			if i == nil {
				continue
			}

			ext := strings.ToLower(filepath.Ext(i.URI))

			subUrl := i.URI
			if isRelativeUrl(i.URI) {
				subUrl, err = joinUrl(list.Url, subUrl)
				if err != nil {
					return nil, err
				}
			}

			switch ext {
			case ".m3u8", "m3u":
				nopt := *opt
				nopt.Url = subUrl
				subList, err := ParseList(&nopt)
				if err != nil {
					return nil, err
				}
				subList.Name = i.Title
				list.lists = append(list.lists, subList)
			default:
				item := new(Item)
				item.Url = subUrl
				list.items = append(list.items, item)
			}
		}

	case m3u8.MASTER:
		l := mList.(*m3u8.MasterPlaylist)
		for _, i := range l.Variants {
			if i == nil {
				continue
			}
			subUrl := i.URI
			if isRelativeUrl(i.URI) {
				subUrl, err = joinUrl(list.Url, subUrl)
				if err != nil {
					return nil, err
				}
			}

			nopt := *opt
			nopt.Url = subUrl
			subList, err := ParseList(&nopt)
			if err != nil {
				return nil, err
			}
			list.lists = append(list.lists, subList)
		}
	}
	return list, nil
}

func isRelativeUrl(path string) bool {
	return !strings.HasPrefix(strings.ToLower(path), "http")
}

func joinUrl(fileUrl, relPath string) (string, error) {
	//get url
	uri, err := url.Parse(fileUrl)
	if err != nil {
		return "", err
	}
	uri.Path = filepath.Join(filepath.Dir(uri.Path), relPath)
	return uri.String(), nil
}
