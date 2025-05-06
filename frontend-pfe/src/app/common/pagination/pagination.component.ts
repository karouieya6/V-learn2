import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
@Component({
  selector: 'app-pagination',
  templateUrl: './pagination.component.html',
  styleUrls: ['./pagination.component.scss'],  
  imports: [RouterModule]
})
export class PaginationComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
