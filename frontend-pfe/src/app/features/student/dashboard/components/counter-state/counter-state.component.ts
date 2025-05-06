import { Component, Input } from '@angular/core';
import { CountUpModule } from 'ngx-countup';

@Component({
  selector: 'dashboard-counter-state',
  standalone: true,
  imports: [CountUpModule],
  templateUrl: './counter-state.component.html',
  styles: ``
})
export class CounterStateComponent {
  @Input() totalCourses: number = 0;
  @Input() totalEnrollments: number = 0;
  @Input() completedCourses: number = 0;
  
}
