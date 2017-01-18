package m3u8

import (
	"encoding/json"
	"io/ioutil"
	"os"
	"path/filepath"
)

type List struct {
	Items     []*Item `json:",omitempty"`
	Lists     []*List `json:",omitempty"`
	EncKey    *Key    `json:",omitempty"`
	Name      string
	Bandwidth int
	Item
}

type Item struct {
	Url      string
	FilePath string
	IsLoad   bool
}

func (l *List) GetItem(i int) *Item {
	if i < 0 || i >= len(l.Items) {
		return nil
	}
	return l.Items[i]
}

func (l *List) SetItem(i int, val *Item) {
	if i < 0 || i >= len(l.Items) {
		return
	}

	l.Items[i].FilePath = val.FilePath
	l.Items[i].Url = val.Url
	l.Items[i].IsLoad = val.IsLoad
}

func (l *List) ItemsSize() int {
	return len(l.Items)
}

func (l *List) ListsSize() int {
	return len(l.Lists)
}

func (l *List) GetList(i int) *List {
	if i < 0 || i >= len(l.Lists) {
		return nil
	}
	return l.Lists[i]
}

func (l *List) GetUrlList() string {
	return l.Url
}

func (l *List) IsLoadList() bool {
	return l.Item.IsLoad
}

func (l *List) SetLoadList(val bool) {
	l.Item.IsLoad = val
}

func (l *List) SaveList(filename string) error {
	buf, err := json.MarshalIndent(l, "", "\t")
	if err != nil {
		return err
	}
	return ioutil.WriteFile(filename, buf, 0666)
}

func LoadList(filename string) (*List, error) {
	buf, err := ioutil.ReadFile(filename)
	if err != nil {
		return nil, err
	}

	l := new(List)
	err = json.Unmarshal(buf, l)
	return l, err
}

func (m *M3U8) SaveList() {
	if m.list != nil {
		m.list.SaveList(filepath.Join(m.opt.TempDir, m.opt.Name) + ".lst")
	}
}

func (m *M3U8) RemoveList() {
	os.Remove(filepath.Join(m.opt.TempDir, m.opt.Name) + ".lst")
}
