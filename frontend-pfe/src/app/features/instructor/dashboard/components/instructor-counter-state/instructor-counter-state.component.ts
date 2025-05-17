import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CountUpModule } from 'ngx-countup';

@Component({
  selector: 'app-instructor-counter-state',
  standalone: true,
  imports: [CommonModule, CountUpModule],
  templateUrl: './instructor-counter-state.component.html',
  styleUrls: ['./instructor-counter-state.component.scss']
})
export class InstructorCounterStateComponent {
  @Input() totalCourses = 0;
  @Input() totalEnrolledStudents = 0;
}
