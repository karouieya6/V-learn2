import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { RouterModule } from '@angular/router';
@Component({
  selector: 'app-student-sidebar',
  standalone: true,
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss'],
  imports: [RouterModule]
})
export class SidebarComponent {
  constructor(private router: Router) {}

  goToDashboard() {
    this.router.navigate(['/student/dashboard']);
  }

  goToMyCourses() {
    this.router.navigate(['/student/course-list']);
  }

  goToResumeCourse() {
    this.router.navigate(['/student/course-resume']);
  }

  goToEditProfile() {
    this.router.navigate(['/student/edit-profile']);
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.router.navigate(['/sign-in']);
  }
}
