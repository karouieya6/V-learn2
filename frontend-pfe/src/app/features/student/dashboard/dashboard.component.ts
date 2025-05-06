import { Component, OnInit } from '@angular/core';
import { CounterStateComponent } from './components/counter-state/counter-state.component';
import { CourseListComponent } from '../course-list/course-list.component';
import { SidebarComponent } from '../../../shared/sidebar/sidebar.component';
import { DashboardService } from '../../../services/dashboard.service';
import { UserService } from '../../../services/user.service'; // adjust path if needed

@Component({
  selector: 'app-dashboard',
  standalone: true,
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
  imports: [CounterStateComponent, CourseListComponent, SidebarComponent],
})
export class DashboardComponent implements OnInit {
  totalCourses = 0;
  totalEnrollments = 0;
  completedCourses = 0;
  userId = 0;

  constructor(private dashboardService: DashboardService, private userService: UserService) {}

  ngOnInit(): void {
    this.userService.getProfile().subscribe({
      next: (user) => {
        this.userId = user.id;

        this.dashboardService.getTotalCourses().subscribe(
          (count) => (this.totalCourses = count)
        );

        this.dashboardService.getTotalEnrollments(this.userId).subscribe(
          (count) => (this.totalEnrollments = count)
        );

        this.dashboardService.getCompletedCourses(this.userId).subscribe(
          (count) => (this.completedCourses = count)
        );
      },
    });
  }
}
