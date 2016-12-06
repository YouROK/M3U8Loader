package m3u8

import (
	"bytes"
	"errors"

	"path/filepath"

	"fmt"

	"github.com/grafov/m3u8"
)

type List struct {
	opts *Options

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

	return l, nil
}
