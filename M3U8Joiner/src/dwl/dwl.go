package dwl

import (
	"dwl/load"
	"dwl/settings"
	"dwl/utils"
	"net/http"
	"os"
	"sync"
)

type Manager struct {
	saveDir string
	loaders []*load.Loader
	mutex   sync.Mutex

	*settings.Settings
}

func OpenManager(savedir string) (*Manager, error) {
	m := new(Manager)
	m.saveDir = savedir
	if !utils.Exists(m.saveDir) {
		os.MkdirAll(m.saveDir, 0777)
	}
	m.loaders = make([]*load.Loader, 0)
	err := m.loadConfig()
	if err != nil {
		return m, err
	}
	return m, nil
}

func (m *Manager) Add(url, name, cookies, useragent string) error {
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
	for _, l := range lists {
		m.addList(l)
	}
	return nil
}

func (m *Manager) Rem(index int) {
	if m.errIndex(index) {
		return
	}
	m.mutex.Lock()
	defer m.mutex.Unlock()
	m.loaders[index].Stop()
	os.Remove(m.getLoaderCfgPath(m.loaders[index]))
	m.loaders[index] = nil
	m.loaders = append(m.loaders[:index], m.loaders[index+1:]...)
}

func (m *Manager) Clean(index int) {
	if m.errIndex(index) {
		return
	}
	m.mutex.Lock()
	defer m.mutex.Unlock()
	loader := m.loaders[index]
	loader.Stop()
	for _, itm := range loader.GetList().Items {
		itm.IsComplete = false
		itm.SetLoadComplete(false)
		itm.Size = 0
		itm.CleanBuffer()
		itm.CleanLoadedBytes()
		itm.StopSpeed()
	}
	os.Remove(m.getLoaderCfgPath(loader))
	m.update(loader)
	os.Remove(loader.GetFilename())
}

func (m *Manager) Len() int {
	return len(m.loaders)
}

func (m *Manager) LoadSubtitles(index int) error {
	if m.errIndex(index) {
		return nil
	}
	m.mutex.Lock()
	defer m.mutex.Unlock()
	return m.loaders[index].LoadSubtitles()
}

func (m *Manager) Load(index int) {
	if m.errIndex(index) {
		return
	}
	m.mutex.Lock()
	defer m.mutex.Unlock()



	go m.loaders[index].Load(m.update)
}

func (m *Manager) Stop(index int) {
	if m.errIndex(index) {
		return
	}
	m.mutex.Lock()
	defer m.mutex.Unlock()
	go m.loaders[index].Stop()
}

func (m *Manager) WaitLoader(index int, timeout int) bool {
	if m.errIndex(index) {
		return true
	}
	m.mutex.Lock()
	m.mutex.Unlock()
	return m.loaders[index].WaitLoading(timeout)
}
