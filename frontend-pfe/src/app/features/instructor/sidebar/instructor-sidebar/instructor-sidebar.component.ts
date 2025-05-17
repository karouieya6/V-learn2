import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-instructor-sidebar',
  standalone: true,
    imports: [CommonModule, RouterModule],

  templateUrl: './instructor-sidebar.component.html',
  styleUrls: ['./instructor-sidebar.component.scss']
})
export class InstructorSidebarComponent {
  constructor(private router: Router) {}

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.router.navigate(['/sign-in']);
  }
}
