package m3u8

type State struct {
	Current int
	Count   int
	Stage   int
	Text    string
	Error   error
}

func (m *M3U8) sendState(curr, count, stage int, text string, err error) {
	m.stateMutext.Lock()
	m.state = &State{curr, count, stage, text, err}
	m.stateMutext.Unlock()
}

func GetState(m *M3U8) *State {
	m.stateMutext.Lock()
	st := m.state
	m.stateMutext.Unlock()
	return st
}
