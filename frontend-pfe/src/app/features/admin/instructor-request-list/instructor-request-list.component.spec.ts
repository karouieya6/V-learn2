import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InstructorRequestListComponent } from './instructor-request-list.component';

describe('InstructorRequestListComponent', () => {
  let component: InstructorRequestListComponent;
  let fixture: ComponentFixture<InstructorRequestListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InstructorRequestListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InstructorRequestListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
