import { Component, Input } from '@angular/core';
import { CountUpModule } from 'ngx-countup';

@Component({
  selector: 'app-admin-counter-state',
  standalone: true,
  imports: [CountUpModule],
  templateUrl: './admin-counter-state.component.html',
  styleUrls: ['./admin-counter-state.component.scss']
})
export class AdminCounterStateComponent {
  @Input() totalUsers = 0;
  @Input() totalCourses = 0;
  @Input() totalEnrollments = 0;
  @Input() instructorRequests = 0;
  @Input() activeInstructors = 0;
  @Input() approvedRequests = 0;
}
