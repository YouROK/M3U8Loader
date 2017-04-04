package load

func (l *Loader) getLoadIndex(startInd int) int {
	for i := startInd; i < len(l.list.Items); i++ {
		p := l.list.Get(i)
		if p.IsLoad && !p.IsLoading && !p.IsComplete && !p.IsFinishLoad {
			return p.Index
		}
	}
	return -1
}

func (l *Loader) isEnd() bool {
	for _, p := range l.list.Items {
		if !p.IsComplete && p.IsLoad {
			return false
		}
	}
	return true
}

/*
func (l *Loader) loadPart() bool {
	l.waitList.Add(1)
	defer l.waitList.Done()
	index := l.getLoadIndex()
	var err error
	if index != -1 {
		for i := 0; i < l.sets.ErrorRepeat; i++ {
			err = l.list.Load(index)
			if err == nil {
				break
			}

			for _, p := range l.list.Items {
				if p.IsLoading && index != p.Index {
					wait := time.Millisecond * 100 * time.Duration(i)
					if wait > time.Second {
						wait = time.Second
					}
					fmt.Println("Err wait", wait.Seconds(), i)
					time.Sleep(wait)
					break
				}
			}
			time.Sleep(time.Millisecond * 200)
		}

		if err == nil {
			err = l.file.WriteAt(l.list)
		}

		if err != nil {
			l.err = err
			l.Stop()
		}
	}
	l.err = err
	return l.err == nil
}

func (l *Loader) isNext() bool {
	if !l.isLoading {
		return false
	}
	threads := 0
	for _, p := range l.list.Items {
		if p.IsLoading {
			threads++
		}

		if threads >= l.sets.Threads {
			return false
		}
	}
	return true
}


func (l *Loader) waitForNext() {
	//calc wait time for next loading
	wait := time.Millisecond * 50
	for _, p := range l.list.Items {
		_, speed := p.GetSpeed()
		bytes := p.LeftBytes()
		if speed == 0 || bytes == 0 {
			continue
		}
		pp := float64(bytes*1000) / float64(speed)
		if wait.Seconds()*1000 > float64(pp) {
			wait = time.Millisecond * time.Duration(pp)
		}
	}
	if wait.Seconds() < 0.100 {
		wait = time.Millisecond * 100
	}
	if wait.Seconds() > 5.0 {
		wait = time.Second * 5
	}
	//wait
	time.Sleep(wait)
}
*/
