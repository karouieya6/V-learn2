import { ComponentFixture, TestBed } from '@angular/core/testing'

import { CourseResumeComponent } from './course-resume.component'

describe('CourseResumeComponent', () => {
  let component: CourseResumeComponent
  let fixture: ComponentFixture<CourseResumeComponent>

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CourseResumeComponent],
    }).compileComponents()

    fixture = TestBed.createComponent(CourseResumeComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it('should create', () => {
    expect(component).toBeTruthy()
  })
})
