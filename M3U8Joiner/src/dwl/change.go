package dwl

import (
	"errors"
	"net/http"
)

func (m *Manager) SetLoaderUrl(url, name string) error {
	lists, err := m.parse(url, name)
	if err != nil {
		return err
	}

	for _, lst := range lists {
		loader := m.findLoaderName(lst.Name)
		if loader != nil {
			loader.GetList().Url = lst.Url
			if loader.GetList().Len() != lst.Len() {
				return errors.New("different list with same name")
			}
			for _, itm := range lst.Items {
				loader.GetList().Get(itm.Index).Url = itm.Url
			}
			m.update(loader)
		}
	}
	return nil
}

func (m *Manager) SetLoaderRange(index int, from, to int) {
	if m.errIndex(index) {
		return
	}
	m.mutex.Lock()
	defer m.mutex.Unlock()

	loader := m.loaders[index]
	for i := 0; i < loader.GetList().Len(); i++ {
		loader.GetList().Get(i).IsLoad = i >= from && i <= to
	}
	m.update(loader)
}

func (m *Manager) SetLoaderUserAgent(index int, useragent string) {
	if m.errIndex(index) {
		return
	}
	m.mutex.Lock()
	defer m.mutex.Unlock()

	loader := m.loaders[index]
	if loader.GetList().Headers == nil {
		loader.GetList().Headers = make(http.Header)
	}
	loader.GetList().Headers.Set("UserAgent", useragent)
	m.update(loader)
}

func (m *Manager) SetLoaderCookies(index int, cookies string) {
	if m.errIndex(index) {
		return
	}
	m.mutex.Lock()
	defer m.mutex.Unlock()

	loader := m.loaders[index]
	if loader.GetList().Headers == nil {
		loader.GetList().Headers = make(http.Header)
	}
	loader.GetList().Headers.Set("Cookie", cookies)
	m.update(loader)
}

func (m *Manager) GetLoaderUserAgent(index int) string {
	if m.errIndex(index) {
		return ""
	}
	m.mutex.Lock()
	defer m.mutex.Unlock()
	loader := m.loaders[index]
	if loader.GetList().Headers == nil {
		return ""
	}
	return loader.GetList().Headers.Get("UserAgent")
}

func (m *Manager) GetLoaderCookies(index int) string {
	if m.errIndex(index) {
		return ""
	}
	m.mutex.Lock()
	defer m.mutex.Unlock()
	loader := m.loaders[index]
	if loader.GetList().Headers == nil {
		return ""
	}
	return loader.GetList().Headers.Get("Cookie")
}

func (m *Manager) GetLoaderRangeFrom(index int) int {
	if m.errIndex(index) {
		return 0
	}
	m.mutex.Lock()
	defer m.mutex.Unlock()
	loader := m.loaders[index]
	itm := loader.GetList().Get(0)
	if itm != nil && itm.IsLoad {
		return 0
	}
	from := loader.GetList().Len() - 1
	for _, itm := range loader.GetList().Items {
		if itm.IsLoad {
			if itm.Index < from {
				from = itm.Index
			}
		}
	}
	return from
}

func (m *Manager) GetLoaderRangeTo(index int) int {
	if m.errIndex(index) {
		return 0
	}
	m.mutex.Lock()
	defer m.mutex.Unlock()
	loader := m.loaders[index]
	itm := loader.GetList().Get(loader.GetList().Len() - 1)
	if itm != nil && itm.IsLoad {
		return loader.GetList().Len() - 1
	}
	to := 0
	for _, itm := range loader.GetList().Items {
		if itm.IsLoad {
			if itm.Index > to {
				to = itm.Index
			}
		}
	}
	return to
}
