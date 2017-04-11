package dwl

import (
	"dwl/list"
	"dwl/load"
	"dwl/settings"
	"dwl/utils"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"path/filepath"
	"sort"
)

const managerConfigFile = "dwl.cfg"

func (m *Manager) loadConfig() error {
	m.readSettings()
	files, err := ioutil.ReadDir(m.saveDir)
	if err != nil {
		return err
	}

	for _, f := range files {
		if filepath.Ext(f.Name()) == ".lst" {
			ldr, err := m.readLoader(filepath.Join(m.saveDir, f.Name()))
			if err != nil {
				fmt.Println("Error load:", f.Name(), err)
			}
			if ldr != nil {
				m.loaders = append(m.loaders, ldr)
			}
		}
	}

	sort.Slice(m.loaders, func(i, j int) bool {
		return m.loaders[i].GetList().Name < m.loaders[j].GetList().Name
	})

	return nil
}

func (m *Manager) saveSettings() error {
	if m.Settings == nil {
		m.Settings = settings.NewSettings()
	}

	buf, err := json.MarshalIndent(m.Settings, "", " ")
	if err != nil {
		return err
	}
	return utils.WriteFileSave(filepath.Join(m.saveDir, managerConfigFile), buf)
}

func (m *Manager) getLoaderCfgPath(loader *load.Loader) string {
	return filepath.Join(m.saveDir, loader.GetList().Name) + ".lst"
}

func (m *Manager) saveLoader(loader *load.Loader) error {
	if m.saveDir == "" || loader.GetList().Name == "" {
		return nil
	}
	fn := m.getLoaderCfgPath(loader)

	list := loader.GetList()
	buf, err := json.MarshalIndent(list, "", " ")
	if err != nil {
		return err
	}
	return utils.WriteFileSave(fn, []byte(buf))
}

func (m *Manager) readSettings() {
	if m.Settings == nil {
		m.Settings = new(settings.Settings)
	}
	if buf, err := ioutil.ReadFile(filepath.Join(m.saveDir, managerConfigFile)); err == nil {
		if json.Unmarshal(buf, m.Settings) == nil {
			return
		}
	}
	m.Settings = settings.NewSettings()
	m.saveSettings()
}

func (m *Manager) readLoader(fn string) (*load.Loader, error) {
	buf, err := ioutil.ReadFile(fn)
	if err != nil {
		return nil, err
	}
	var llist *list.List = new(list.List)
	err = json.Unmarshal(buf, llist)
	if err != nil {
		return nil, err
	}

	if llist != nil && llist.Len() > 0 {
		//при открытии сохранения, могут быть дыры, тогда нужно загружать всё с дыры
		find := false
		for n := 0; n < llist.Len(); n++ {
			itm := llist.Get(n)
			itm.SetLoading(false)                                          //not loading on open
			itm.StopSpeed()                                                //reset speed
			if !find && (!itm.IsComplete || itm.Size == 0) && itm.IsLoad { //found a hole
				find = true
				itm.IsComplete = false
				itm.Size = 0
				continue
			}
			if find { //closed a hole
				itm.IsComplete = false
				itm.Size = 0
			}
		}
	}

	return load.NewLoader(m.Settings, llist), nil
}

func (m *Manager) update(loader *load.Loader) {
	err := m.saveLoader(loader)
	if err != nil {
		fmt.Println("Error save list config", err, loader.GetList().Url, m.getLoaderCfgPath(loader))
	}
}
