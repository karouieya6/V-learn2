import { Component, OnInit, } from '@angular/core';
import { RouterModule } from '@angular/router';
@Component({
  selector: 'app-blog-sidebar',
  templateUrl: './blog-sidebar.component.html',
  styleUrls: ['./blog-sidebar.component.scss'],
  imports: [RouterModule]
})
export class BlogSidebarComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
