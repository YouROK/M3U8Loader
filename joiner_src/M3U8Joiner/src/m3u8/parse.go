package m3u8

import (
	"bytes"
	"errors"
	"io/ioutil"
	"loader"
	"net/url"
	"path/filepath"
	"strings"

	"github.com/grafov/m3u8"
)

func ParseLocalList(opt *loader.HttpOpts) (*List, error) {
	name := opt.Url
	if strings.HasPrefix(name, "file://") {
		name = name[7:]
	}
	buf, err := ioutil.ReadFile(name)
	if err != nil {
		return nil, err
	}
	mList, mType, err := m3u8.DecodeFrom(bytes.NewBuffer(buf), false)
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
			if isRelativeUrl(i.URI) {
				return nil, errors.New("Local files not support: " + i.URI)
			}
			ext := strings.ToLower(filepath.Ext(i.URI))
			switch ext {
			case ".m3u8", "m3u":
				nopt := *opt
				nopt.Url = i.URI
				subList, err := ParseList(&nopt)
				if err != nil {
					return nil, err
				}
				subList.Name = i.Title
				list.Lists = append(list.Lists, subList)
			default:
				item := new(Item)
				item.Url = i.URI
				list.Items = append(list.Items, item)
			}
		}
	case m3u8.MASTER:
		l := mList.(*m3u8.MasterPlaylist)
		for _, i := range l.Variants {
			if i == nil {
				continue
			}
			if isRelativeUrl(i.URI) {
				return nil, errors.New("Local files not support: " + i.URI)
			}

			nopt := *opt
			nopt.Url = i.URI
			subList, err := ParseList(&nopt)
			if err != nil {
				return nil, err
			}
			list.Lists = append(list.Lists, subList)
		}
	}
	return list, nil
}

func ParseList(opt *loader.HttpOpts) (*List, error) {
	http := loader.NewHttp(opt)
	err := http.Connect()
	defer func() {
		http.Close()
		http = nil
	}()
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
				list.Lists = append(list.Lists, subList)
			default:
				item := new(Item)
				item.Url = subUrl
				list.Items = append(list.Items, item)
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
			subList.Bandwidth = int(i.Bandwidth)
			list.Lists = append(list.Lists, subList)
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
