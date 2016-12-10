package m3u8

type List struct {
	items    []*Item
	lists    []*List
	Name     string
	bandwidt int
	Item
}

type Item struct {
	Url      string
	FilePath string
	IsLoad   bool
}

func (l *List) GetItem(i int) *Item {
	if i < 0 || i >= len(l.items) {
		return nil
	}
	return l.items[i]
}

func (l *List) SetItem(i int, val *Item) {
	if i < 0 || i >= len(l.items) {
		return
	}

	l.items[i].FilePath = val.FilePath
	l.items[i].Url = val.Url
	l.items[i].IsLoad = val.IsLoad
}

func (l *List) ItemsSize() int {
	return len(l.items)
}

func (l *List) ListsSize() int {
	return len(l.lists)
}

func (l *List) GetList(i int) *List {
	if i < 0 || i >= len(l.lists) {
		return nil
	}
	return l.lists[i]
}

func (l *List) IsLoadList() bool {
	return l.Item.IsLoad
}

func (l *List) SetLoadList(val bool) {
	l.Item.IsLoad = val
}
