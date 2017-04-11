package list

import (
	"bytes"
	"dwl/crypto"
	"dwl/stats"
	"dwl/utils"
	"encoding/hex"
	"fmt"
	"github.com/grafov/m3u8"
	"net/http"
	"strings"
)

type ParseList struct {
	url, name string
	header    http.Header
	lists     []*List
}

func ParseUrl(url, name string, header http.Header) (*ParseList, error) {
	p := new(ParseList)
	p.url = url
	p.name = name
	p.header = header
	lists, err := p.parse(url)
	if err != nil {
		return nil, err
	}
	p.lists = lists
	return p, nil
}

func (p *ParseList) GetLists() []*List {
	return p.lists
}

func (p *ParseList) parse(url string) ([]*List, error) {
	buf, err := utils.ReadBuf(url, p.header)
	if err != nil {
		return nil, err
	}

	plist, _, err := m3u8.DecodeFrom(bytes.NewBuffer(buf), false)
	if err != nil {
		return nil, err
	}

	retLists := make([]*List, 0)
	baseUrl, err := utils.DirUrl(url)
	if err != nil {
		return nil, err
	}

	switch mlist := plist.(type) {
	case *m3u8.MediaPlaylist:
		{
			list := new(List)
			list.Url = url
			list.Name = p.name
			list.Headers = p.header

			if mlist.Key != nil {
				keyUrl, err := utils.JoinUrl(baseUrl, mlist.Key.URI)
				if err != nil {
					return nil, err
				}
				key, err := p.parseKey(keyUrl, mlist.Key.IV)
				if err != nil {
					return nil, err
				}
				list.EncKey = key
			}

			for _, i := range mlist.Segments {
				if i == nil || i.URI == "" {
					continue
				}

				itemUrl, err := utils.JoinUrl(baseUrl, i.URI)
				if err != nil {
					return nil, err
				}

				//if item is parse
				if strings.HasSuffix(strings.ToLower(i.URI), "m3u8") || strings.HasSuffix(strings.ToLower(i.URI), "m3u") {
					lists, err := p.parse(itemUrl)
					if err != nil {
						return nil, err
					}
					if len(lists) > 0 {
						for k, itm := range lists {
							itm.Bandwidth = k
							itm.Name = i.Title
							if itm.Name == "" {
								itm.Name = p.name + "." + fmt.Sprint(itm.Bandwidth)
							}
						}
						retLists = append(retLists, lists...)
					}
				} else {
					// if items in media
					itm := stats.Item{}
					itm.Url = itemUrl
					itm.IsLoad = true
					itm.Index = len(list.Items)
					list.Items = append(list.Items, &itm)
				}
			}
			if len(list.Items) > 0 {
				retLists = append(retLists, list)
			}
		}
	case *m3u8.MasterPlaylist:
		{
			//Parse lists of parse
			for _, i := range mlist.Variants {
				if i == nil || i.URI == "" {
					continue
				}
				itemUrl, err := utils.JoinUrl(baseUrl, i.URI)
				if err != nil {
					return nil, err
				}
				lists, err := p.parse(itemUrl)
				if err != nil {
					return nil, err
				}
				if len(lists) > 0 {
					for k, lst := range lists {
						lst.Bandwidth = int(i.Bandwidth)
						if lst.Bandwidth == 0 {
							lst.Bandwidth = k
						}
						lst.Name = i.Name
						if lst.Name == "" {
							lst.Name = p.name + "." + fmt.Sprint(lst.Bandwidth)
						}
					}
					retLists = append(retLists, lists...)
				}
			}
		}
	}
	return retLists, nil
}

func (p *ParseList) parseKey(url, iv string) (*crypto.Key, error) {
	buf, err := utils.ReadBuf(url, p.header)
	if err != nil {
		return nil, err
	}
	var ivbuf []byte
	if iv != "" {
		if strings.HasPrefix(strings.ToLower(iv), "0x") {
			iv = iv[2:]
		}

		ivbuf, err = hex.DecodeString(iv)
		if err != nil {
			return nil, err
		}
	}
	k := new(crypto.Key)
	k.IV = ivbuf
	k.Key = buf

	return k, err
}
