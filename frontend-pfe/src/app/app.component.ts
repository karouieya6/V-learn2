import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { RouterModule } from '@angular/router';
import { Router, NavigationEnd } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { FooterComponent } from '../app/common/footer/footer.component'; // or header-two
import { HeaderThreeComponent } from './pages/Home/header-three/header-three.component';
import { SignUpAreaComponent } from './auth/sign-up/sign-up-area/sign-up-area.component';
import { SignInAreaComponent } from './auth/sign-in/sign-in-area/sign-in-area.component';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, HttpClientModule,CommonModule, HeaderThreeComponent, FooterComponent, SignUpAreaComponent, RouterModule, SignInAreaComponent,ReactiveFormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  title = 'frontend-app';
  showHeaderFooter = true;
  isAuthPage = false;
  constructor(private router: Router) {
    this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.isAuthPage = event.url.includes('sign-in') || event.url.includes('sign-up');
      }
    });
  }
  ngOnInit(): void {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        const url = event.urlAfterRedirects;
        this.showHeaderFooter = !(
          url.includes('/sign-in') || url.includes('/sign-up')
        );
      }
    });
  }





}
