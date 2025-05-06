import { Component, OnInit,Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Router } from '@angular/router';
@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss']
})
export class FooterComponent implements OnInit {

  @Input () footerPadd : string | undefined

  constructor(public router: Router) {}

  ngOnInit(): void {
  }
  isAuthPage(): boolean {
    const url = this.router.url;
    return url === '/sign-in' || url === '/sign-up';
  }
}
