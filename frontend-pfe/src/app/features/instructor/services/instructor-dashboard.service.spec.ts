import { TestBed } from '@angular/core/testing';

import { InstructorDashboardService } from './instructor-dashboard.service';

describe('InstructorDashboardService', () => {
  let service: InstructorDashboardService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(InstructorDashboardService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
