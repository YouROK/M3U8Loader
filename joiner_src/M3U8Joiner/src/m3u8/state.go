package m3u8

import (
	"fmt"
	"time"
)

type State struct {
	Current int
	Count   int
	Stage   int
	Text    string
	Error   error
}

func (s *State) String() string {
	stage := ""
	switch s.Stage {
	case Stage_Stoped:
		stage = "stoped"
	case Stage_Error:
		stage = "error"
	case Stage_Finished:
		stage = "finished"
	case Stage_JoinSegments:
		stage = "joining"
	case Stage_LoadingContent:
		stage = "loading content"
	case Stage_LoadingList:
		stage = "loading list"
	case Stage_RemoveTemp:
		stage = "remove temp"
	}

	return fmt.Sprintln(s.Current, s.Count, stage, s.Text, "\n", s.Error)
}

func (m *M3U8) sendState(curr, count, stage int, text string, err error) {
	m.stateMutext.Lock()
	if m.state == nil || cap(m.state) < count {
		m.state = make(chan *State, count*3+100)
	}
	m.stateMutext.Unlock()
	m.state <- &State{curr, count, stage, text, err}
}

func GetState(m *M3U8) *State {
	m.stateMutext.Lock()
	if m.state == nil {
		m.state = make(chan *State, m.opt.Threads*10)
	}
	m.stateMutext.Unlock()
	timer := time.NewTimer(time.Millisecond * 150)
	select {
	case st := <-m.state:
		return st
	case <-timer.C:
		return nil
	}
}
