import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { SidebarComponent } from '../../shared/sidebar/sidebar.component';
import { AdminSidebarComponent } from '../../features/admin/sidebar/admin-sidebar/admin-sidebar.component';
@Component({
  selector: 'app-account-layout',
  templateUrl: './account-layout.component.html',
  styleUrls: ['./account-layout.component.scss'],
  imports: [
    RouterModule,
    SidebarComponent,
    AdminSidebarComponent,
    CommonModule // ✅ Your sidebar must be standalone too
  ]
})
export class AccountLayoutComponent implements OnInit {
  userRole: string = '';

  ngOnInit(): void {
    const user = localStorage.getItem('user');
    if (user) {
      const parsed = JSON.parse(user);
      this.userRole = parsed.roles?.[0] || ''; // assuming roles is an array
    }
  }
}
