package dwl

type LoaderInfo struct {
	Url            string
	Name           string
	Threads        int
	Speed          int64
	LoadedBytes    int64
	LoadedDuration float64
	Duration       float64

	Completed    int
	LoadingCount int
	All          int

	Status int
	Error  string
}

func (m *Manager) GetLoaderInfo(index int) *LoaderInfo {
	if m.errIndex(index) {
		return nil
	}
	m.mutex.Lock()
	defer m.mutex.Unlock()

	list := m.loaders[index].GetList()
	if list == nil {
		return nil
	}

	li := new(LoaderInfo)
	li.Url = list.Url
	li.Name = list.Name
	li.Status = m.loaders[index].Status()
	li.All = len(list.Items)
	if m.loaders[index].Error() != nil {
		li.Error = m.loaders[index].Error().Error()
	}
	for _, itm := range list.Items {
		if itm.IsLoad {
			if itm.IsComplete {
				li.Completed++
			}
			loaded := itm.GetLoadedBytes()
			if loaded == 0 {
				loaded = itm.Size
			}
			if itm.Duration > 0 {
				if itm.IsComplete {
					li.LoadedDuration += itm.Duration
				}
				li.Duration += itm.Duration
			}
			li.LoadedBytes += loaded
			li.Speed += itm.GetSpeed()
			li.LoadingCount++
			if itm.IsLoading() {
				li.Threads++
			}
		}
	}
	return li
}

func (m *Manager) GetLoaderFileName(index int) string {
	if m.errIndex(index) {
		return ""
	}
	m.mutex.Lock()
	defer m.mutex.Unlock()
	return m.loaders[index].GetFilename()
}

//////////////////////////////////////////////////////////////////////////////////

func (m *Manager) errIndex(i int) bool {
	if i >= 0 && i < len(m.loaders) {
		return false
	}
	return true
}
