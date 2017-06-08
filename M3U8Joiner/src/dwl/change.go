package dwl

import (
	"errors"
	"net/http"
	"path/filepath"
)

func (m *Manager) SetLoaderUrl(url, name, cookies, useragent string) error {
	var header = make(http.Header)
	if useragent != "" {
		header.Add("UserAgent", useragent)
	} else if m.Useragent != "" {
		header.Add("UserAgent", m.Useragent)
	}
	if cookies != "" {
		header.Add("Cookie", cookies)
	} else if m.Cookies != "" {
		header.Add("Cookie", m.Cookies)
	}

	lists, err := m.parse(url, name, header)
	if err != nil {
		return err
	}

	for _, lst := range lists {
		loader := m.findLoaderName(lst.Name)
		if loader != nil {
			if loader.GetList().Len() != lst.Len() {
				return errors.New("different list with same name")
			}
			loader.GetList().Url = lst.Url
			loader.GetList().Headers = header
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

func (m *Manager) SetSubtitles(index int, subs string) {
	if m.errIndex(index) {
		return
	}
	m.mutex.Lock()
	defer m.mutex.Unlock()

	loader := m.loaders[index]
	loader.GetList().Subtitles = subs
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

func (m *Manager) GetSubtitlesUrl(index int) string {
	if m.errIndex(index) {
		return ""
	}
	m.mutex.Lock()
	defer m.mutex.Unlock()
	loader := m.loaders[index]
	return loader.GetList().Subtitles
}

func (m *Manager) GetSubtitlesFile(index int) string {
	if m.errIndex(index) {
		return ""
	}
	m.mutex.Lock()
	defer m.mutex.Unlock()
	loader := m.loaders[index]
	if loader.GetList().Subtitles == "" {
		return ""
	}

	ext := filepath.Ext(loader.GetList().Subtitles)
	if ext == "" {
		ext = ".srt"
	}
	return filepath.Join(m.Settings.DownloadPath, loader.GetList().Name) + ext
}
