import { Component, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { NgbDropdownModule } from '@ng-bootstrap/ng-bootstrap';
import { UserService } from '../../../services/user.service';

@Component({
  standalone: true,
  selector: 'app-header-three',
  templateUrl: './header-three.component.html',
  styleUrls: ['./header-three.component.scss'],
  imports: [CommonModule, RouterModule, NgbDropdownModule],
})
export class HeaderThreeComponent implements OnInit {
  user: any = null;
  isLoggedIn: boolean = false;
  headerSticky: boolean = false;
  searchBar: boolean = false;
  showSidebar: boolean = false;
  userImageUrl: string = '';
  defaultAvatar: string = 'assets/img/user/userprofile.png';
  userProfilePic: string = this.defaultAvatar;
  
  userName: string = '';
  userEmail: string = '';
  mode: string = 'light';

  @HostListener('window:scroll', [])
  onWindowScroll() {
    this.headerSticky = window.pageYOffset > 50;
  }
  handleSearch() {
    this.searchBar = !this.searchBar;
  }

  handleSearchClose() {
    this.searchBar = false;
  }

  handleSidebar() {
    this.showSidebar = true;
    document.querySelector('.sidebar')?.classList.add('active');
  }

  handleSidebarClose() {
    this.showSidebar = false;
    document.querySelector('.sidebar')?.classList.remove('active');
  }

  constructor(private router: Router, private userService: UserService) {}
  
  ngOnInit(): void {
    const savedTheme = localStorage.getItem('theme') || 'light';
    this.changeTheme(savedTheme);
  
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    if (user && user.email) {
      this.user = user;
      this.isLoggedIn = true;
      this.userName = user.firstName + ' ' + user.lastName || 'User';
      this.userEmail = user.email;
      this.userProfilePic = user.imageUrl || 'assets/img/user/userprofile.png';
    }
  
    this.checkLoginStatus(); // fetch fresh copy
  }
  

  checkLoginStatus() {
    const token = localStorage.getItem('token');
  
    if (!token) {
      this.isLoggedIn = false;
      this.user = null;
      this.userName = '';
      this.userEmail = '';
      this.userProfilePic = 'assets/img/user/userprofile.png';
      return;
    }
  
    this.userService.getProfile().subscribe({
      next: (res) => {
        this.user = res;
        this.isLoggedIn = true;
        this.userName = res.username || 'User';
        this.userEmail = res.email || 'example@gmail.com';
        this.userProfilePic = this.userProfilePic = res.imageUrl || this.userProfilePic;
        localStorage.setItem('user', JSON.stringify(res)); 
      },
      error: (err) => {
        this.isLoggedIn = false;
        this.user = null;
        this.userName = '';
        this.userEmail = '';
        this.userProfilePic = 'assets/img/user/userprofile.png';
        if (err.status === 401) {
          localStorage.removeItem('token');
          this.router.navigate(['/sign-in']);
        }
      },
    });
  }

  isAuthPage(): boolean {
    const url = this.router.url;
    return url === '/sign-in' || url === '/sign-up' || url === '/forgot-password' || url === '/reset-password';
  }

  logout() {
    this.userService.logout().subscribe({
      next: () => {
        this.isLoggedIn = false;
        this.user = null;
        this.userName = '';
        this.userEmail = '';
        this.userProfilePic = 'assets/img/user/userprofile.png';
        this.router.navigate(['/']);
      },
      error: (err: unknown) => {
        console.error('Logout failed:', err);
        this.isLoggedIn = false;
        this.user = null;
        this.userName = '';
        this.userEmail = '';
        this.userProfilePic = 'assets/img/user/userprofile.png';
        window.location.href = '/';

      },
    });
  }

  changeTheme(theme: string) {
    this.mode = theme;
    document.body.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
  }
}