package m3u8

import (
	"bytes"
	"fmt"
	"io/ioutil"
	"loader"
	"path/filepath"
	"strings"

	"github.com/grafov/m3u8"
)

func (l *List) parse() error {

	var buf []byte
	var err error

	if l.Item.Url != "" {
		hopt := *l.opts.HttpOpts
		hopt.Url = l.Item.Url
		http := loader.NewHttp(&hopt)
		err = http.Connect()
		if err != nil {
			return err
		}
		buf, err = ioutil.ReadAll(http)
	} else if l.Item.TempFilePath != "" {
		buf, err = ioutil.ReadFile(l.Item.TempFilePath)
	}

	if err != nil {
		return err
	}

	pList, lType, err := m3u8.Decode(bytes.NewBuffer(buf), false)
	if err != nil {
		return err
	}

	switch lType {
	case m3u8.MEDIA:
		list := pList.(*m3u8.MediaPlaylist)
		for i, l := range list.Segments {
			if strings.HasPrefix(strings.ToLower(filepath.Ext(l.URI)), ".m3u") {

				NewList(l.URI)
			}

		}
	case m3u8.MASTER:
		list := pList.(*m3u8.MasterPlaylist)
		fmt.Println(list)
	}
	return nil
}
