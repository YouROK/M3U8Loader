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
	defer m.stateMutext.Unlock()
	if m.stateChan != nil {
		m.stateChan <- State{curr, count, stage, text, err}
	}
}

func (m *M3U8) PollState() State {
	if m.stateChan == nil {
		m.stateChan = make(chan State, m.opt.Threads)
	}
	return <-m.stateChan
}
