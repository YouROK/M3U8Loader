package dwl

import (
	"dwl/load"
	"dwl/settings"
	"dwl/utils"
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

func (m *Manager) Add(url, name string) error {
	lists, err := m.parse(url, name)
	if err != nil {
		return err
	}
	for _, l := range lists {
		//l.IndexSort = len(m.loaders)
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
	m.mutex.Lock()
	defer m.mutex.Unlock()
	return len(m.loaders)
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

func (m *Manager) WaitLoader(index int) {
	if m.errIndex(index) {
		return
	}
	m.mutex.Lock()
	m.mutex.Unlock()
	m.loaders[index].WaitLoading()
}
