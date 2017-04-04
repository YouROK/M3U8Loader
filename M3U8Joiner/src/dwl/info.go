package dwl

type LoaderInfo struct {
	Url         string
	Name        string
	Threads     int
	Speed       int64
	LoadedBytes int64

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
			li.LoadedBytes += itm.Size
			_, spd := itm.GetSpeed()
			li.Speed += int64(spd)
			li.LoadingCount++
			if itm.IsLoading {
				li.Threads++
			}
		} else {

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