package dwl

import (
	"dwl/list"
	"dwl/load"
	"net/http"
)

func (m *Manager) parse(url, name string) ([]*list.List, error) {
	var header http.Header = make(http.Header)
	if m.Useragent != "" {
		header.Add("UserAgent", m.Useragent)
	}
	if m.Cookies != "" {
		header.Add("Cookie", m.Cookies)
	}

	parseList, err := list.ParseUrl(url, name, header)
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

