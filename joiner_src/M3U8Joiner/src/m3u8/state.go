package m3u8

import (
	"time"
)

type State struct {
	Current int
	Count   int
	Stage   int
	Text    string
	Error   error
}

func (m *M3U8) sendState(curr, count, stage int, text string, err error) {
	m.stateMutext.Lock()
	if m.state == nil && m.opt.Threads > 0 {
		m.state = make(chan *State, m.opt.Threads*500)
	}
	m.stateMutext.Unlock()
	m.state <- &State{curr, count, stage, text, err}
}

func GetState(m *M3U8) *State {
	m.stateMutext.Lock()
	if m.state == nil && m.opt.Threads > 0 {
		m.state = make(chan *State, m.opt.Threads*500)
	}
	m.stateMutext.Unlock()
	timer := time.NewTimer(time.Second)
	select {
	case st := <-m.state:
		return st
	case <-timer.C:
		return nil
	}
}
