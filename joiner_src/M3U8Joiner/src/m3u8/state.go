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
	if m.stateChan == nil {
		m.stateChan = make(chan *State, m.opt.Threads*10)
	}
	m.stateMutext.Unlock()
	m.stateChan <- &State{curr, count, stage, text, err}
}

func PollState(m *M3U8) *State {
	m.stateMutext.Lock()
	ch := m.stateChan
	m.stateMutext.Unlock()
	if ch == nil {
		return nil
	}
	st := <-ch
	return st
}
