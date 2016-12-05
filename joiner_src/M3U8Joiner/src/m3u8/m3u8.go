package m3u8

import (
	"bytes"
	"errors"

	"path/filepath"

	"fmt"

	"github.com/grafov/m3u8"
)

type List struct {
	opts  *Options
	items []*Item
	lists []*List
	Item
}

type Item struct {
	Name         string
	Url          string
	TempFilePath string
}

func NewList(path string, opt *Options) (*List, error) {

	if opt.TempDir == "" {
		return nil, errors.New("Temp directory is empty")
	}

	if opt.Name == "" {
		return nil, errors.New("Name is empty")
	}

	if opt.OutFilePath == "" {
		return nil, errors.New("Out path is empty")
	}

	l := new(List)
	l.Item.Url = path
	l.Item.TempFilePath = filepath.Join(opt.TempDir, opt.Name+".m3u8") // e.g. /tmp/TempDir/Name.m3u8
	l.Item.Name = opt.Name

	l.parse()

	return nil, nil
}

func parse(path string, buf []byte) (*List, error) {
	pList, lType, err := m3u8.Decode(bytes.NewBuffer(buf), false)
	if err != nil {
		return nil, err
	}
	retList := new(List)
	switch lType {
	case m3u8.MEDIA:
		list := pList.(*m3u8.MediaPlaylist)
		for i, l := range list.Segments {
			fmt.Println(i, l)

		}
	case m3u8.MASTER:
		list := pList.(*m3u8.MasterPlaylist)
		fmt.Println(list)
	}
	return retList, nil
}

func load(path string, opt *Options) []byte {
	return nil
}
