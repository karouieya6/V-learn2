import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InstructorCounterStateComponent } from './instructor-counter-state.component';

describe('InstructorCounterStateComponent', () => {
  let component: InstructorCounterStateComponent;
  let fixture: ComponentFixture<InstructorCounterStateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InstructorCounterStateComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InstructorCounterStateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
