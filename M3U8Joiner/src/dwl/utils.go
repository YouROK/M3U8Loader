package dwl

import (
	"dwl/list"
	"dwl/load"
	"net/http"
	"strconv"
	"unicode"
)

func (m *Manager) parse(url, name string, header http.Header) ([]*list.List, error) {
	parseList, err := list.ParseUrl(url, name, m.Settings.LoadItemsSize, header)
	if err != nil {
		return nil, err
	}
	return parseList.GetLists(), nil
}

func (m *Manager) addList(lst *list.List) {
	m.mutex.Lock()
	defer m.mutex.Unlock()
	loader := load.NewLoader(m.Settings, lst)
	m.loaders = append(m.loaders, loader)
	m.update(loader)
}

func (m *Manager) findLoaderName(name string) *load.Loader {
	m.mutex.Lock()
	defer m.mutex.Unlock()
	for _, l := range m.loaders {
		if l.GetList().Name == name {
			return l
		}
	}
	return nil
}

func GetNumbersStr(val string) int {
	arrNums := make([]string, 0)
	pos := 0
	isFind := false
	for _, n := range val {
		if unicode.IsDigit(n) {
			isFind = true
			if len(arrNums)-1 < pos {
				arrNums = append(arrNums, "")
			}
			arrNums[pos] += string(n)
		}
		if !unicode.IsDigit(n) {
			if isFind {
				pos++
			}
			isFind = false
		}
	}

	if len(arrNums) == 0 {
		return -1
	}

	num := 0
	for i, n := range arrNums {
		coff := (len(arrNums) - 1 - i) * 1000
		if coff == 0 {
			coff = 1
		}
		nn, _ := strconv.Atoi(n)
		num += nn * coff
	}

	return num
}
