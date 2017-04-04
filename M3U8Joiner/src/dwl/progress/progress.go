package progress

import (
	"time"
)

type Progress []DownloadProgress

type DownloadProgress struct {
	LoadedByte int64 `json:"-"`

	DeltaBytes uint64    `json:"-"`
	RealSpeed  uint64    `json:"-"`
	AverSpeed  uint64    `json:"Speed"`
	LastTime   time.Time `json:"-"`
	StartTime  time.Time `json:"-"`

	LoadingTime time.Duration `json:"-"`
	ConnectTime time.Duration `json:"-"`
	IsLoading   bool          `json:"IsLoading"`
	IsComplete  bool          `json:"IsComplete"`
}

func (s *DownloadProgress) StartSpeed() {
	s.RealSpeed = 0
	s.AverSpeed = 0
	s.DeltaBytes = 0
	s.LoadedByte = 0
	s.StartTime = time.Now()
	s.LastTime = time.Now()
}

func (s *DownloadProgress) StopSpeed() {
	s.RealSpeed = 0
	s.AverSpeed = 0
	s.DeltaBytes = 0
}

func (s *DownloadProgress) GetSpeed() (uint64, uint64) {
	return s.RealSpeed, s.AverSpeed
}

func (s *DownloadProgress) MessureSpeed(realc int) {
	s.DeltaBytes += uint64(realc)
	s.LoadedByte += int64(realc)

	delta := time.Since(s.StartTime).Seconds()
	if time.Since(s.LastTime).Seconds() > 0.1 {
		s.LastTime = time.Now()
		lstSpeed := s.RealSpeed
		s.RealSpeed = uint64(float64(s.DeltaBytes) / delta)
		s.AverSpeed = (s.AverSpeed + (s.RealSpeed+lstSpeed)/2) / 2
	}
	if time.Since(s.StartTime).Seconds() > 5 {
		s.StartTime = time.Now()
		s.LastTime = time.Now()
		s.DeltaBytes = 0
	}
}
