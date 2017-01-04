package m3u8

import (
	"time"
)

func (m *M3U8) Speed() int64 {
	delta := time.Since(m.lsmt).Seconds()
	if delta > 1 {
		m.speed = int64(float64(m.bytesInSecond) / delta)
		m.lsmt = time.Now()
		m.bytesInSecond = 0
	}
	return m.speed
}

func (m *M3U8) cleanSpeed() {
	m.speed = 0
	m.bytesInSecond = 0
	m.lsmt = time.Now()
}

func (m *M3U8) messureSpeed(realc int) {
	m.bytesInSecond += int64(realc)
}
